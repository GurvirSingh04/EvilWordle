// Main Part 2 about Evil Wordle
//===============================


object M2 { 

import io.Source
import scala.util._


def get_wordle_list(url: String) : List[String] = {
    try {
        val source = Source.fromURL(url)
        val words = source.getLines.toList
        source.close()
        words
    } catch {
        case _: Exception => List()
  }
}

def removeN[A](xs: List[A], elem: A, n: Int): List[A] = {
  def helper(list: List[A], remaining: Int, acc: List[A]): List[A] = {
    list match {
      case Nil => acc.reverse
      case x :: xs if x == elem && remaining > 0 => helper(xs, remaining - 1, acc)
      case x :: xs => helper(xs, remaining, x :: acc)
    }
  }
  if (n <= 0) xs
  else helper(xs, n, Nil)
}

abstract class Tip
case object Absent extends Tip
case object Present extends Tip
case object Correct extends Tip


def pool(secret: String, word: String): List[Char] = {
  (secret.toList.zip(word.toList)).foldLeft(List[Char]())((acc, pair) => 
    if (pair._1 != pair._2) pair._1 :: acc else acc
  ).reverse
}

def aux(secret: List[Char], word: List[Char], pool: List[Char]): List[Tip] = {
  (secret, word) match {
    case (Nil, Nil) => Nil
    case (s, Nil)  => Nil
    case (Nil, w)  => Nil
    case (s :: ss, w :: ws) =>
      if (s == w) 
        Correct :: aux(ss, ws, pool)
      else if (pool.contains(w)) 
        Present :: aux(ss, ws, removeN(pool, w, 1))
      else 
        Absent :: aux(ss, ws, pool)
  }
}

def score(secret: String, word: String) : List[Tip] = {
    aux(secret.toList, word.toList, pool(secret, word))
}

def eval(t: Tip) : Int = {
    t match {
        case Absent => 0
        case Present => 1
        case Correct => 10
    }
}

def iscore(secret: String, word: String) : Int = {
    score(secret, word).map(eval).sum
}

def lowest(secrets: List[String], word: String, current: Int, acc: List[String]) : List[String] = {
    secrets match {
        case secret :: rest =>
            val score = iscore(secret, word)
            if (score < current){
                lowest(rest, word, score, List(secret))
            }
            else if (score == current){
                lowest(rest, word, current, acc :+ secret)
            }
            else{
                lowest(rest, word, current, acc)
            }
        case _=>acc

    }
}

def evil(secrets: List[String], word: String) : List[String] = {
    lowest(secrets, word, Int.MaxValue, List())
}

def frequencies(secrets: List[String]): Map[Char, Double] = {
  val frequencyMap = secrets.mkString.groupBy(_.toLower).view.mapValues(_.length.toDouble).toMap
  frequencyMap.view.mapValues(freq => 1 - (freq / frequencyMap.values.sum)).toMap
}

def rank(frqs: Map[Char, Double], s: String) : Double = {
    s.toList match {
        case char :: rest=>
            frqs(char) + rank(frqs, rest.mkString)
        case Nil => 0.0
    }
}

def ranked_evil(secrets: List[String], word: String) : List[String] = {
    val frequenciesMap = frequencies(secrets)
    val evils = evil(secrets, word)

    evils.foldLeft(List.empty[String] -> Double.MinValue) {
        case ((maxWords, maxRank), evilWord) =>
        val currentRank = rank(frequenciesMap, evilWord)

        if (currentRank > maxRank) {
            List(evilWord) -> currentRank
        } else if (currentRank == maxRank) {
            (evilWord :: maxWords, maxRank)
        } else {
            maxWords -> maxRank
        }
    }._1.reverse
}
}
