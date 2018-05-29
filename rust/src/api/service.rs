use futures::future;
use futures::future::Future;
use futures::Stream;
use hyper;
use hyper::{Body, Method, StatusCode};
use hyper::server::{Http, Request, Response, Service};
use regex::Regex;
use std::net::{IpAddr, SocketAddr};
use std::sync::Arc;
use storage::Engine;

pub struct ApiService {
    engine: Arc<Engine + Send + Sync>,
    bind_address: String,
    bind_port: u16,
}

impl ApiService {
    pub fn new(engine: Arc<Engine + Send + Sync>, bind_address: String, bind_port: u16) -> ApiService {
        ApiService {
            engine,
            bind_address,
            bind_port,
        }
    }

    pub fn start(&self) -> () {
        let address: IpAddr = self.bind_address.parse().unwrap();
        let socket: SocketAddr = SocketAddr::new(address, self.bind_port);
        let handler = ServiceHandler { engine: self.engine.to_owned() };

        let server = Http::new()
            .bind(
                &socket,
                move || Ok(handler.clone()),
            )
            .unwrap();

        server.run().unwrap();
    }
}

#[derive(Clone)]
struct ServiceHandler {
    engine: Arc<Engine>
}

impl Service for ServiceHandler {
    type Request = Request;
    type Response = Response;
    type Error = hyper::Error;
    type Future = Box<Future<Item=Self::Response, Error=Self::Error>>;

    fn call(&self, req: Self::Request) -> Self::Future {
        let method = req.method().clone();
        let key = Self::get_key_from_path(req.path());

        let response: Self::Future = match (method, key) {
            (Method::Get, Some(key)) => self.get(key),
            (Method::Put, Some(key)) => self.put(key, req.body()),
            (Method::Post, Some(key)) => self.put(key, req.body()),
            (Method::Delete, Some(key)) => self.delete(key),
            _ => self.default()
        };

        response
    }
}

impl ServiceHandler {
    fn get_key_from_path(path: &str) -> Option<String> {
        lazy_static! {
        static ref URI_REGEX: Regex = Regex::new("^/(?P<key>[^/]+)").unwrap();
    }

        URI_REGEX.captures(path)
            .map(|captures| captures["key"].to_string())
            .and_then(|key| if key.is_empty() { None } else { Some(key) })
    }

    fn get(&self, key: String) -> Box<Future<Item=Response, Error=hyper::Error>> {
        let mut response = Response::new();
        match self.engine.get(key) {
            Some(result) => response.set_body(result),
            None => response.set_status(StatusCode::NotFound),
        };

        Box::new(future::ok(response))
    }

    fn put(&self, key: String, body: Body) -> Box<Future<Item=Response, Error=hyper::Error>> {
        let engine = self.engine.to_owned();

        let future = body
            .concat2()
            .map(move |chunk| {
                let data = chunk.iter()
                    .cloned()
                    .collect::<Vec<u8>>();

                if engine.put(key, data) {
                    Response::new()
                } else {
                    Response::new().with_status(StatusCode::InternalServerError)
                }
            });

        Box::new(future)
    }

    fn delete(&self, key: String) -> Box<Future<Item=Response, Error=hyper::Error>> {
        let response = if self.engine.put(key, vec![]) {
            Response::new()
        } else {
            Response::new().with_status(StatusCode::InternalServerError)
        };

        Box::new(future::ok(response))
    }

    fn default(&self) -> Box<Future<Item=Response, Error=hyper::Error>> {
        Box::new(future::ok(Response::new().with_status(StatusCode::NotFound)))
    }
}
