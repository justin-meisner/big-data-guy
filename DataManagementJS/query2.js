// query2 : unwind friends and create a collection called 'flat_users' 
// where each document has the following schema:
/*
{
  user_id:xxx
  friends:xxx
}
*/

//Justin Meisner and Eliot Sollinger
//EECS 484 P3

function unwind_friends(dbname){
    db = db.getSiblingDB(dbname);
    // TODO: unwind friends
    db.createCollection("flat_users");
	db.users.aggregate( [ { $unwind: "$friends" }, {$project : { _id : 0, "user_id" : 1, "friends" : 1}}, { $out : "flat_users"} ] );
}
