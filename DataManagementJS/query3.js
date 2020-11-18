//query3
//create a collection "cities" to store every user that lives in every city
//Each document(city) has following schema:
/*
{
  _id: city
  users:[userids]
}
*/

//Justin Meisner and Eliot Sollinger
//EECS 484 P3

function cities_table(dbname) {
    db = db.getSiblingDB(dbname);
    db.createCollection("cities");
    db.users.aggregate( { $group : { _id : "$current.city" , users : { $push : "$user_id" } } }, { $out : "cities" } );
 
}
