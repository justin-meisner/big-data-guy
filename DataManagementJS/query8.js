// query 8: Find the city average friend count per user using MapReduce
// Using the same terminology in query7, we are asking you to write the mapper,
// reducer and finalizer to find the average friend count for each city.

//Justin Meisner and Eliot Sollinger
//EECS 484 P3


var city_average_friendcount_mapper = function() {
  // implement the Map function of average friend count
  emit( this.hometown.city, { tally : 1 , num_friends : this.friends.length } );

};

var city_average_friendcount_reducer = function(key, values) {
  // implement the reduce function of average friend count

  var result = { tally : 0, num_friends : 0 }
  for(var i = 0; i < values.length; ++i){
  	result.tally += values[i].tally;
  	result.num_friends += values[i].num_friends;
  }
  return result;

};

var city_average_friendcount_finalizer = function(key, reduceVal) {
  // We've implemented a simple forwarding finalize function. This implementation 
  // is naive: it just forwards the reduceVal to the output collection.
  // You will need to change it for this query to work

 return reduceVal.num_friends / reduceVal.tally;
}
