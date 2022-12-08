import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}

import scala.io._
import org.apache.spark.rdd._
import org.apache.log4j.Logger
import org.apache.log4j.Level

import java.util
import scala.collection._

object AveragePlacement {
  def main(args: Array[String]): Unit = {
    //set up spark
    val conf = new SparkConf().setAppName("NameOfApp").setMaster("local[4]")
    val sc = new SparkContext(conf)
    //set up logger
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)


    //read all files in directory, starting with "na1player" and ending with ".out"
    val file_path = "riot_files/na1player1"
    val na_player = sc.textFile(file_path + "/na1player*.out")
      .map(line =>
        (line.replaceAll("\"", "")
          .replaceAll("\\(", "").replaceAll("\\)", "")))

    //(key = (character, rarity, tier), value = placement)
    val na_player_units_placement = na_player.map { line =>
      val matchID_placement_key = (line.split(", ")(0), line.split(", ")(1).toInt)
      val rest_of_data = line.split(", ").drop(2)
      (matchID_placement_key, rest_of_data)
    }
      //filter out records with no units data
      .filter(_._2.length != 0)
      //remove duplicates by key
      .reduceByKey((a, b) => a)
      //map to (key = (character, rarity, tier), value = placement)
      .map { x =>
        val placement = x._1._2
        val data = x._2.map { x =>
          val character = x.split(",")(0).split("_")(1)
          val rarity = x.split(",")(1).toInt
          val tier = x.split(",")(2).toInt
          ((character, rarity, tier), placement)
        }
        (data)
      }
      //flatten the 2d array to 1d array
      .flatMap(x => x)

    //get the average placement for each unit
    val na_player_units_placement_avg = na_player_units_placement.combineByKey(
        (v: Int) => (v, 1),
        (acc: (Int, Int), v: Int) => (acc._1 + v, acc._2 + 1),
        (acc1: (Int, Int), acc2: (Int, Int)) => (acc1._1 + acc2._1, acc1._2 + acc2._2)
      )
      .map{ case (key, value) => (key, value._1 / value._2.toDouble)}
      .sortBy(x => (x._1._1, x._1._3))

 //comes into effect before weight T*R combo
    val Rarity = List(1.10, 1.08, 1.05, 1.02, 1.00, 0.97, 0.94, 0.90)
    val Tier = List(1,0.9,0.8)

    val weights = List(1.000, 1.333, 1.528, 1.667, 1.774, 1.862, 1.936, 2.000)

    val na_player_units_placement_avg_weight = na_player_units_placement_avg.map{ x =>
      val character = x._1._1
      val rarity = x._1._2
      val tier = x._1._3
      val placement = x._2.toDouble
      val weight = weights(rarity)
      ((character, tier), (rarity, placement, weight))
    }

    val input_char_rarity = sc.textFile("./UserInput.txt").map(x => ((x.split(", ")(0), x.split(", ")(1).trim().toInt), null))

    val input_player_units_placement_avg_weight = na_player_units_placement_avg_weight.join(input_char_rarity)

    println("JOINED DATA: ")
    input_player_units_placement_avg_weight.collect().foreach{ x=>
      //println(x._2)
      val character = x._1._1
      val tier = x._1._2
      val rarity = x._2._1._1
      val placement = x._2._1._2
      val weight = x._2._1._3
      println(s"$character" + s", $tier" + s", $rarity" + f", $placement%1.5f" +f", $weight%1.5f")
    }

    val rarityFactor = List(1.20, 1.15, 1.10, 1.05, 0.98, 0.90, 0.82, 0.75)
    val tierFactor = List(0.00, 1.2, 1.1, 0.95)

    val weightplacement_sumweights = input_player_units_placement_avg_weight.collect().aggregate(0.0, 0.0)(
      (x,y) => {
        val rf = rarityFactor(y._2._1._1).toDouble
        val tf = tierFactor(y._1._2).toDouble
        (x._1 + y._2._1._2 * y._2._1._3 * rf * tf, x._2 + y._2._1._3 * rf * tf)
      },
      (x,y) => (x._2 + y._2, x._1 + y._1))

    println(weightplacement_sumweights._1/ weightplacement_sumweights._2)

  }
}
