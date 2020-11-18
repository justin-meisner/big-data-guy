// query6 : Find the Average friend count per user for users
//
// Return a decimal variable as the average user friend count of all users
// in the users document.

//Justin Meisner and Eliot Sollinger
//EECS 484 P3

function find_average_friendcount(dbname){
  db = db.getSiblingDB(dbname)
  // TODO: return a decimal number of average friend count

  db.createCollection("friend_sums");

  db.users.aggregate( { $project : { _id : 0, "user_id" : 1, "friends" :  1 } } , { $out : "friend_sums" } );

  var CursorA = db.friend_sums.find();

  var total = 0;
  var tally = 0;
  CursorA.forEach( user => {
  	total += user.friends.length;
  	++tally;
  })

return total/tally;



}
