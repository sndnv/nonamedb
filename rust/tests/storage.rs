extern crate nonamedb;
extern crate robots;
extern crate env_logger;

use nonamedb::storage::Engine;
use nonamedb::storage::engines::memory::MemoryEngine;

#[test]
fn memory_engine_should_successfully_handle_requests() {
    env_logger::init().unwrap();

    let test_key = "some key".to_string();
    let test_value = "some value".as_bytes().to_vec();
    let updated_test_value = "some updated value".as_bytes().to_vec();

    let system = {
        let system = robots::actors::ActorSystem::new("test_system".to_owned());
        system.spawn_threads(4);
        std::sync::Arc::new(system)
    };

    let engine = MemoryEngine::new(system.clone());

    // should fail to retrieve missing data
    assert_eq!(engine.get(test_key.clone()), None);

    // should successfully add and retrieve data
    assert!(engine.put(test_key.clone(), test_value.clone()));
    assert_eq!(engine.get(test_key.clone()), Some(test_value.clone()));

    // should successfully update and retrieve updated data
    assert!(engine.put(test_key.clone(), updated_test_value.clone()));
    assert_eq!(engine.get(test_key.clone()), Some(updated_test_value.clone()));

    // should successfully remove data
    assert!(engine.put(test_key.clone(), vec![]));
    assert_eq!(engine.get(test_key.clone()), None);

    system.shutdown();
}
