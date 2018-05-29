use robots::actors::{Actor, ActorCell, ActorContext, ActorRef, ActorSystem, Props};
use std::any::Any;
use std::collections::hash_map::Entry;
use std::collections::HashMap;
use std::sync::{Arc, Mutex};
use storage::Engine;

pub struct MemoryEngine {
    store: ActorRef,
    system: Arc<ActorSystem>,
}

impl MemoryEngine {
    pub fn new(system: Arc<ActorSystem>) -> MemoryEngine {
        let props = Props::new(Arc::new(EngineActor::new), ());
        let actor = system.actor_of(props.clone(), "MemoryEngine::EngineActor".to_owned());
        MemoryEngine { store: actor, system }
    }
}

impl Engine for MemoryEngine {
    fn get(&self, key: String) -> Option<Vec<u8>> {
        let future = self.system.ask(
            self.store.clone(),
            Message::Get { key },
            "MemoryEngine::Get".to_owned(),
        );

        match self.system.extract_result(future) {
            Response::Data { value } => value,
            _ => None
        }
    }

    fn put(&self, key: String, value: Vec<u8>) -> bool {
        let future = self.system.ask(
            self.store.clone(),
            Message::Put { key, value },
            "MemoryEngine::Put".to_owned(),
        );

        match self.system.extract_result(future) {
            Response::Done => true,
            _ => false
        }
    }
}

struct EngineActor {
    store: Mutex<HashMap<String, Vec<u8>>>,
}

impl EngineActor {
    pub fn new(_: ()) -> EngineActor {
        EngineActor { store: Mutex::new(HashMap::new()) }
    }
}

impl Actor for EngineActor {
    fn receive(&self, message: Box<Any>, context: ActorCell) {
        if let Ok(message) = Box::<Any>::downcast::<Message>(message) {
            match *message {
                Message::Get { ref key } => {
                    let store = self.store.lock().unwrap();
                    let result = store.get(key).map(|value| value.clone());

                    debug!(
                        "[GET] Value with key [{}] {}",
                        if result.is_some() { "found" } else { "not found" },
                        key
                    );

                    context.complete(context.sender(), Response::Data { value: result });
                }

                Message::Put { ref key, ref value } => {
                    let mut store = self.store.lock().unwrap();
                    if value.is_empty() {
                        debug!("[PUT] Removing value with key [{}]", key);
                        store.remove(key);
                    } else {
                        match store.entry(key.clone()) {
                            Entry::Occupied(mut entry) => {
                                entry.insert(value.clone());
                                debug!("[PUT] Updating value with key [{}]", key);
                            }

                            Entry::Vacant(entry) => {
                                entry.insert(value.clone());
                                debug!("[PUT] Adding value with key [{}]", key);
                            }
                        }
                    }

                    context.complete(context.sender(), Response::Done);
                }
            }
        }
    }
}

#[derive(Clone, PartialEq)]
enum Message {
    Get { key: String },
    Put { key: String, value: Vec<u8> },
}

#[derive(Clone, PartialEq)]
enum Response {
    Data { value: Option<Vec<u8>> },
    Done,
}
