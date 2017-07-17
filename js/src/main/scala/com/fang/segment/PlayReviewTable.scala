package com.fang.segment

import com.fang.GamePlayJson
import com.thoughtworks.binding.Binding.Vars
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.Node
import com.fang.ImplicitConvert._

object PlayReviewTable {
  @dom def apply(games: Vars[GamePlayJson]): Binding[Node] = {
    <table class="table table-striped">
      <thead>
        <tr>
          <th>Player1</th>
          <th>Player2</th>
          <th>Rule</th>
          <th>Winner</th>
          <th>Watch Now</th>
        </tr>
      </thead>
      <tbody>
        {
        for(item <- games) yield <tr>
          <td>{item.first_user}</td>
          <td>{item.second_user}</td>
          <td>{item.rule}</td>
          <td>{item.first_win match {
            case Some(true) => item.first_user
            case Some(false) => item.second_user
            case None => "-"
          }}</td>
          {val url = "#game/" + item.id
          if(item.status == GamePlayJson.PLAYING){
            <td><a class="btn btn-primary btn-sm" href={url}>Observe</a></td>
          }else{
            <td><a class="btn btn-info btn-sm" href={url}>Review</a></td>
          }
          }
        </tr>
        }
      </tbody>
    </table>
  }
}
