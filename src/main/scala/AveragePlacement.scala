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
    val file_path = "src/main/input_files/Project"
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
    val na_player_units_placement_avg = na_player_units_placement
      .combineByKey(
        (v: Int) => (v, 1),
        (acc: (Int, Int), v: Int) => (acc._1 + v, acc._2 + 1),
        (acc1: (Int, Int), acc2: (Int, Int)) => (acc1._1 + acc2._1, acc1._2 + acc2._2)
      )
      .map{ case (key, value) => (key, value._1 / value._2.toDouble)}
      .sortBy(x => (x._1._1, x._1._3))

    na_player_units_placement_avg.collect.foreach { x =>
      val character = x._1._1
      val tier = x._1._3
      val rarity = x._1._2
      val placement = x._2
      println(s"$character" + s", $rarity" + s", $tier" + f", $placement%1.5f")
    }
    }
  }
}
