# riot_dataset_analysis

Use of Riot Developer API to collect dataset
Dataset analysis performed via Spark cluster-computation written in Scala language.



Our scala takes a JSON input of match data and collects it into data rows of

  ```unit_name, tier, placement```
  
  
  Where the combination of unit_name, tier is the key for our RDD, and placement is the value. 
  
  
We then use an rdd to reduce our data, and get the average placement of a unit at tier 1, 2, and 3.

This data is put into an RDD where we can access an average placement, and the weight of the unit, to perform a weighted average across any number of units inputted, producing the expected placement.
