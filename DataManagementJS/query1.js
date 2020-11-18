// query1 : find users whose hometown citys the specified city. 

//Justin Meisner and Eliot Sollinger
//EECS 484 P3

function find_user(city, dbname){
    db = db.getSiblingDB(dbname);
    var results = [];
    // TODO: return a Javascript array of user_ids. 
    // db.users.find(...);

    var myCursor = db.users.find({"hometown.city" : city}).forEach(user => {
    	results.push(user.user_id);
    });

    
    // cursor.forEach( function(item){
    // 	System.out.println(item.hometown.city)
    // 	if (item.hometown.city == city){
    // 		results.push(user.user_id)
    // 	}
    // });
    // See test.js for a partial correctness check.  
    // The result will be an array of integers. The order does not matter.                                                             
    return results;
}
