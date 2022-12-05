object Main {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)
    println("Hello world!")
  }

  def SimpleLinearReg(): Unit = {
    val conf = new SparkConf().setAppName("App").setMaster("local[4]")
    val sc = new SparkContext(conf);
    // load from file
    val xy = sc.textFile("./test.txt").map(x => (x.split(", ")(0).toDouble, x.split(", ")(1).toDouble));
    // y = mx + b
    // y - y1 = m(x - x1)
    // input key-value pairs (x, y)
    val input_size = xy.count();
    val x_bar = xy.keys.sum * 1.0/ input_size;
    val y_bar = xy.values.sum * 1.0/ input_size;
    // m = (xi - x_bar) * (yi - y_bar) / (xi -  x_bar)^2

    val m = xy.map({case(xi, yi) => ((xi - x_bar) * (yi - y_bar))}).sum() / xy.map({case(xi, y) => pow(xi - x_bar, 2)}).sum();
    val b = y_bar - m * x_bar;


    println("x_bar: " + x_bar);
    println("y_bar: "+y_bar);
    println("slope: "+m);
    println("y-intercept: "+b);
  }
}