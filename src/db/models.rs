use crate::db::schema::servers;

#[derive(Queryable, Insertable, Clone, Copy, Debug)]
#[table_name = "servers"]
pub struct Server {
    pub id: i64,
    pub banter: bool,
}

impl Server {
    pub fn new(id: i64) -> Server {
        Server {
            id,
            banter: false
        }
    }
}