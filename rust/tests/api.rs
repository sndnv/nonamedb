extern crate nonamedb;
extern crate robots;
extern crate env_logger;

extern crate futures;
extern crate hyper;
extern crate tokio_core;

use futures::{Future, Stream};
use hyper::{Body, Client, Method, Request, StatusCode, Uri};
use hyper::client::HttpConnector;
use nonamedb::api::service::ApiService;
use nonamedb::storage::engines::memory::MemoryEngine;
use robots::actors::{Actor, ActorCell, ActorSystem, Props};
use std::any::Any;
use std::sync::Arc;
use tokio_core::reactor::Core;

#[test]
fn api_service_should_successfully_handle_requests() {
    env_logger::init().unwrap();

    let test_address = "127.0.0.1".to_string();
    let test_port = 8090;
    let test_authority = format!("{}:{}", test_address, test_port);

    let test_key = "somekey".to_string();
    let test_value = "some value".as_bytes().to_vec();
    let updated_test_value = "some updated value".as_bytes().to_vec();
    let test_uri = format!("http://{}/{}", test_authority, test_key);

    let system = {
        let system = ActorSystem::new("test_system".to_owned());
        system.spawn_threads(4);
        std::sync::Arc::new(system)
    };

    let engine = Arc::new(MemoryEngine::new(system.clone()));

    let api = Arc::new(
        ApiService::new(
            engine,
            test_address,
            test_port,
        )
    );

    let props = Props::new(Arc::new(TestApiActor::new), api.to_owned());
    let api_actor = system.actor_of(props.clone(), "TestApiActor".to_owned());
    let _api_run_future = system.ask(api_actor, Message::Start, "TestApiActor::Start".to_owned());

    let mut client = TestClient::new();

    // should reject invalid requests
    assert_eq!(client.get(format!("http://{}", test_authority)), (None, StatusCode::NotFound));

    // should fail to retrieve missing data
    assert_eq!(client.get(test_uri.clone()), (None, StatusCode::NotFound));

    // should successfully add and retrieve data
    assert_eq!(client.put(test_uri.clone(), test_value.clone()), StatusCode::Ok);
    assert_eq!(client.get(test_uri.clone()), (Some(test_value.clone()), StatusCode::Ok));

    // should successfully retrieve updated data
    assert_eq!(client.post(test_uri.clone(), updated_test_value.clone()), StatusCode::Ok);
    assert_eq!(client.get(test_uri.clone()), (Some(updated_test_value.clone()), StatusCode::Ok));

    // should fail to retrieve deleted data
    assert_eq!(client.delete(test_uri.clone()), StatusCode::Ok);
    assert_eq!(client.get(test_uri.clone()), (None, StatusCode::NotFound));

    system.shutdown();
}

struct TestApiActor {
    api: Arc<ApiService>,
}

impl TestApiActor {
    fn new(api: Arc<ApiService>) -> TestApiActor {
        TestApiActor { api }
    }
}

impl Actor for TestApiActor {
    fn receive(&self, message: Box<Any>, _: ActorCell) {
        if let Ok(message) = Box::<Any>::downcast::<Message>(message) {
            match *message {
                Message::Start => self.api.start()
            };
        }
    }
}

#[derive(Clone, PartialEq)]
enum Message {
    Start,
}

struct TestClient {
    client: Client<HttpConnector, Body>,
    core: Core,
}

impl TestClient {
    fn new() -> TestClient {
        let core = Core::new().unwrap();
        let client = Client::new(&core.handle());

        TestClient { client, core }
    }

    fn get(&mut self, uri: String) -> (Option<Vec<u8>>, StatusCode) {
        let uri: Uri = uri.parse().unwrap();

        let request =
            self.client.get(uri.clone()).and_then(|response| {
                let status = response.status().clone();
                response.body().concat2().map(move |chunk| {
                    let data = chunk.iter()
                        .cloned()
                        .collect::<Vec<u8>>();

                    (data, status)
                })
            });

        match self.core.run(request) {
            Ok((result, status)) => {
                match status {
                    StatusCode::NotFound => (None, StatusCode::NotFound),
                    StatusCode::Ok => {
                        if !result.is_empty() {
                            (Some(result), StatusCode::Ok)
                        } else {
                            println!("[GET] Received empty data from [{}]", uri);
                            (None, StatusCode::InternalServerError)
                        }
                    }
                    _ => {
                        println!("[GET] Received unexpected status code from [{}]: [{}]", uri, status);
                        (None, StatusCode::InternalServerError)
                    }
                }
            }
            Err(e) => {
                println!("[GET] Error encountered while getting data from [{}]: [{}]", uri, e);
                (None, StatusCode::InternalServerError)
            }
        }
    }

    fn put(&mut self, uri: String, data: Vec<u8>) -> StatusCode {
        self.request_with_data(Method::Put, uri, data)
    }

    fn post(&mut self, uri: String, data: Vec<u8>) -> StatusCode {
        self.request_with_data(Method::Post, uri, data)
    }

    fn delete(&mut self, uri: String) -> StatusCode {
        self.request_with_data(Method::Delete, uri, vec![])
    }

    fn request_with_data(&mut self, method: Method, uri: String, data: Vec<u8>) -> StatusCode {
        let uri: Uri = uri.parse().unwrap();

        let mut request = Request::new(method.clone(), uri.clone());
        request.set_body(data);

        let request = self.client.request(request);

        match self.core.run(request) {
            Ok(result) => {
                result.status()
            }
            Err(e) => {
                println!("[{}] Error encountered while getting data from [{}]: [{}]", method, uri, e);
                StatusCode::InternalServerError
            }
        }
    }
}
