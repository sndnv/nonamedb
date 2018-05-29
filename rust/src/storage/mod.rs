pub mod engines;

pub trait Engine {
    fn get(&self, key: String) -> Option<Vec<u8>>;
    fn put(&self, key: String, value: Vec<u8>) -> bool;
}
