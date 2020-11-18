// find the oldest friend for each user who has a friend. 
// For simplicity, use only year of birth to determine age, if there is a tie, use the one with smallest user_id
// return a javascript object : key is the user_id and the value is the oldest_friend id
// You may find query 2 and query 3 helpful. You can create selections if you want. Do not modify users collection.
//
//You should return something like this:(order does not matter)
//{user1:userx1, user2:userx2, user3:userx3,...}

//Justin Meisner and Eliot Sollinger
//EECS 484 P3


// TODO: implement oldest friends
  // return an javascript object described above


function oldest_friend(dbname){
	db = db.getSiblingDB(dbname);
  
	db.createCollection("flat_people");

	//creates flat friends lists for each user
	db.users.aggregate( [ { $unwind: "$friends" }, { $project : { _id : 0, user_id : 1, friends : 1 } }, { $out : "flat_people" } ] );

	//creates both orientations of all friend pairs
	db.flat_people.find().forEach(myDoc1 => {
    	db.friend_pairs.insert( [ { "user1_id" : myDoc1.user_id, "user2_id" : myDoc1.friends } ]);
    	db.friend_pairs.insert( [ { "user1_id" : myDoc1.friends, "user2_id" : myDoc1.user_id } ]); 
  });

	//creates dictionary of all YOBs indexed at each user_id
	var yob_dict = {};

  	db.users.find().forEach(myDoc2 => {
      yob_dict[myDoc2.user_id] = myDoc2.YOB;
    } );

    var oldest_person = {};


  	db.users.find().forEach(myDoc3 => {
      db.friend_pairs.find( { user1_id : myDoc3.user_id } ).forEach(myDoc4 => {
      	if (!(myDoc4.user1_id in oldest_person)){
      		oldest_person[myDoc4.user1_id] = myDoc4.user2_id;
      	}

        if ( yob_dict[oldest_person[myDoc4.user1_id]] > yob_dict[myDoc4.user2_id] ){
        	oldest_person[myDoc4.user1_id] = myDoc4.user2_id;
        } 
        
        if ( ( yob_dict[oldest_person[myDoc4.user1_id]] == yob_dict[myDoc4.user2_id])) {
        	if ( oldest_person[myDoc4.user1_id] > myDoc4.user2_id ){
            	oldest_person[myDoc4.user1_id] = myDoc4.user2_id;
        	}
        }
      
    }  
    );
  }
);
	return oldest_person;
}
