use std::{env};
use diesel::{ExpressionMethods, insert_into, PgConnection, QueryDsl, RunQueryDsl};
use diesel::r2d2::{ConnectionManager, Pool};
use diesel_migrations::run_pending_migrations;

use dotenv::dotenv;
use crate::db::models::Server;
use crate::db::schema::servers::{id};
use crate::db::schema::servers::dsl::servers;
use crate::error::Error;

pub mod schema;
pub mod models;

pub fn establish_connection() -> Pool<ConnectionManager<PgConnection>> {
    dotenv().ok();

    let database_url = env::var("DATABASE_URL")
        .expect("DATABASE_URL must be set");

    let manager = ConnectionManager::<PgConnection>::new(database_url.clone());

    let pool = Pool::builder().max_size(15).build(manager)
        .expect(&format!("Error connecting to {}", &database_url));

    match run_pending_migrations(&*pool.get().unwrap()) {
        Ok(_) => println!("Successfully ran database migrations"),
        Err(e) => {
            println!("Failed to run database migrations: {}", e)
        }
    }

    pool
}

pub fn get_server(guild_id: u64, conn: &mut PgConnection) -> Result<Server, Error> {
    let server = servers.filter(id.eq(guild_id as i64))
        .load::<Server>(conn)?;

    if server.len() == 0 {
        // No server created, create one
        println!("Creating server for guild {}", guild_id);
        let guild = Server::new(guild_id as i64);
        insert_into(servers)
            .values(guild.clone())
            .execute(conn)?;

        return Ok(guild)
    }

    Ok(*server.get(0).unwrap())
}
