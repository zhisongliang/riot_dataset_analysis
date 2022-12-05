# riot_dataset_analysis

Use of Riot Developer API to collect dataset
Dataset analysis performed via Spark cluster-computation written in Scala language.



Our scala takes a JSON input of match data and collects it into data rows of

  ```placement, unit_name, rarity, tier```
  
We then use an rdd to reduce our data to 3 per unit, and get the average placement of a unit at tier 1, 2, and 3.

This data is put into a dictionary where we can access an average placement, and the weight of the unit, to output the average placement of multiple units together
