import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import spray.json._
import DefaultJsonProtocol._ // if you don't supply your own Protocol (see below)
import java.io._

object riot_tft_api_parsing {
  def main(args: Array[String]) {
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)

    val conf = new SparkConf().setAppName("NameOfApp").setMaster("local[4]")
    val sc = new SparkContext(conf)
    //parameter to setMaster tells us how to distribute
    // the data, e.g., 4 partitions on the localhost
    //don't use setMaster if running on cluster

    // Example Json Parsing
//    val source = """{ "some": "JSON source" }"""
//    val jsonAst = source.parseJson // or JsonParser(source)
//    val json = jsonAst.prettyPrint // or .compactPrint
//    print(json)

    val na_player1 = sc.textFile("src/main/input_files/Project/na1player1_match_history.txt")
//    na_player1.foreach(line => {
//    val fixed = line.replaceAll("'", "\"")
//    val something = fixed.parseJson
//    print(something.prettyPrint)
//    })
//val match_and_players = sc.textFile("src/main/input_files/Project/na1player1_match_history.txt").map(line => {
//    val fixed = line.replaceAll("'", "\"")
//    val something = fixed.parseJson
//    something.asJsObject.getFields("metadata").map(a => a.asJsObject().getFields("match_id"))
//})
    for( i<- 1 to 250) {
      val matchPre = "src/main/input_files/Project/na1player"
      val matchCount = i
      val matchSuf = "_match_history.txt"
      val matchHistory = sc.textFile(matchPre + matchCount.toString + matchSuf).map(line => line.replaceAll("'", "\"").parseJson).persist()

      val match_and_players = matchHistory.map(a => {
        val metadata = a.asJsObject.getFields("metadata").flatMap(a => a.asJsObject().getFields("match_id", "participants"))
        metadata
      })

      //    match_and_players.collect().foreach(println(_))

      val match_and_champs = matchHistory.map(a => {
        val match_id = a.asJsObject.getFields("metadata").flatMap(a => a.asJsObject.getFields("match_id"))
        val participants = a.asJsObject.getFields("info").flatMap(a => a.asJsObject.getFields("participants"))
        List(match_id, participants)
      })

      //    match_and_champs.collect().foreach(println(_))
      //Gets matchid, player placement + units json
      val match_and_player_info = matchHistory.map(matchhist => {
        val match_id = matchhist.asJsObject.getFields("metadata").flatMap(metadata => metadata.asJsObject.getFields("match_id"))
        val participants = matchhist.asJsObject.getFields("info")
          .flatMap(info => info.asJsObject.getFields("participants"))
        val thing = participants.map(a => a.toString()).mkString(" ")
        val asparagus = thing.stripMargin.parseJson.asInstanceOf[JsArray]
        val aspFields = asparagus.elements
          .map(element => {
            val placement = element.asJsObject.getFields("placement")
            val units = element.asJsObject.getFields("units")
            val broccoli = units.map(a => a.toString()).mkString(" ").stripMargin.parseJson.asInstanceOf[JsArray]
            val brocFields = broccoli.elements.map(element => {
              val char_id = element.asJsObject.getFields("character_id")
              //            val items = element.asJsObject.getFields("items")
              val rarity = element.asJsObject.getFields("rarity")
              val tier = element.asJsObject.getFields("tier")
              (char_id.flatMap(a => a.toString()).mkString(""), rarity.flatMap(a => a.toString()).mkString(""), tier.flatMap(a => a.toString()).mkString(""))
            })
            (placement.head, brocFields)
          })
        (match_id, aspFields)
      })
      val suffix = ".out"
      val pw = new PrintWriter(new File(matchPre + matchCount.toString + suffix))
      match_and_player_info.collect().foreach(x => {
        val matchid = x._1
        val players = x._2
        val thing = players.map(player => {
          val units = player._2.toList.mkString(", ")
          player._1 + ", " + units
        })
        thing.foreach(x => {
          println(matchid(0) + ", " + x)
          pw.println(matchid(0) + ", " + x)
        })
      })
      pw.close()
    }

  }
}