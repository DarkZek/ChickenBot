use crate::db::schema::servers;

#[derive(Queryable, Insertable, Clone, Copy, Debug)]
#[table_name = "servers"]
pub struct Server {
    pub id: i64,
    pub banter: bool,
    pub distance_conversion: bool,
    pub summarize: bool,
}

impl Server {
    pub fn new(id: i64) -> Server {
        Server {
            id,
            banter: true,
            distance_conversion: true,
            summarize: true,
        }
    }
}