
// query 4: find user pairs (A,B) that meet the following constraints:
// i) user A is male and user B is female
// ii) their Year_Of_Birth difference is less than year_diff
// iii) user A and B are not friends
// iv) user A and B are from the same hometown city
// The following is the schema for output pairs:
// [
//      [user_id1, user_id2],
//      [user_id1, user_id3],
//      [user_id4, user_id2],
//      ...
//  ]
// user_id is the field from the users collection. Do not use the _id field in users.
  
//Justin Meisner and Eliot Sollinger
//EECS 484 P3


function suggest_friends(year_diff, dbname) {
    db = db.getSiblingDB(dbname);
    var pairs = [];
    // TODO: implement suggest friends
    // Return an array of arrays.

    var CursorA = db.users.find( { gender : "male"} );
    CursorA.forEach(user => {
    	var CursorB = db.users.find( { gender : "female" } );
    	CursorB.forEach(userb  => {
    		if ((Math.abs(user.YOB - userb.YOB) < year_diff) && (Math.abs(userb.YOB - user.YOB) < year_diff)){
    			if (user.hometown.city == userb.hometown.city){
    				if ((user.friends.indexOf(userb.user_id) == -1) && (userb.friends.indexOf(user.user_id) == -1)){
    					pairs.push([user.user_id, userb.user_id]);
    				}
    			}
    		}
    	})
    })
    return pairs;
}
