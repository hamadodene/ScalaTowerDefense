package Model.Grid

import Configuration.DefaultConfig.{TILE_END_POSITION_ID, TILE_HEIGHT_PX, TILE_START_POSITION_ID, TILE_WIDTH_PX}
import Logger.LogHelper
import Model.Grid.Tiles.{TileType, TileTypes}
import scalafx.scene.paint.Color
import scala.collection.mutable.ArrayBuffer

trait Grid {

  def gridDrawingInfo: ArrayBuffer[(Color, Int, Int)]

  def tile(x: Int, y: Int): Tile

  def grid: Array[Array[Tile]]

  def tileStartOrEnd(filter: TileTypes.TileType): Option[Tile]
}

object Grid {

  sealed private case class GridImpl(difficulty: Int) extends Grid with LogHelper {

    private val _grid: Array[Array[Tile]] = makeGrid()

    private def makeGrid(): Array[Array[Tile]] = {

      val pathMaker = PathMaker()

      val rawGrid: Array[Array[Int]] = difficulty match {
        case 1 => pathMaker.execute(pathMaker.simplePath)
        case 2 => pathMaker.execute(pathMaker.normalPath)
        case 3 => pathMaker.execute(pathMaker.hardPath)
        case 0 => pathMaker.execute(pathMaker.customPath)
      }

      if (!pathValidator(rawGrid)) System.exit(1)

      generateTileGrid(rawGrid)
    }

    private def generateTileGrid(rawGrid: Array[Array[Int]]): Array[Array[Tile]] = {

      val arrayOfTile = Array.ofDim[Tile](rawGrid.length, rawGrid(0).length)

      for (y <- rawGrid.indices) {
        for (x <- rawGrid(y).indices) {
          rawGrid(y)(x) match {
            case 0 => arrayOfTile(y)(x) = Tile(x * TILE_HEIGHT_PX, y * TILE_WIDTH_PX, TileType(TileTypes.Grass))
            case 1 => arrayOfTile(y)(x) = Tile(x * TILE_HEIGHT_PX, y * TILE_WIDTH_PX, TileType(TileTypes.Path))
            case 2 => arrayOfTile(y)(x) = Tile(x * TILE_HEIGHT_PX, y * TILE_WIDTH_PX, TileType(TileTypes.StartTile))
            case 3 => arrayOfTile(y)(x) = Tile(x * TILE_HEIGHT_PX, y * TILE_WIDTH_PX, TileType(TileTypes.EndTile))
            case _ => arrayOfTile(y)(x) = Tile(x * TILE_HEIGHT_PX, y * TILE_WIDTH_PX, TileType(TileTypes.Nothing))
          }
        }
      }
      arrayOfTile
    }

    private def pathValidator(path: Array[Array[Int]]): Boolean = {
      if (containSingleStartOrEnd(path, TILE_START_POSITION_ID) &&
        containSingleStartOrEnd(path, TILE_END_POSITION_ID)) true else false
    }

    private def containSingleStartOrEnd(path: Array[Array[Int]], position: Int): Boolean = {
      path.flatMap(_.toSeq).groupBy(identity).view.mapValues(_.length)(position) match {
        case 1 => true
        case _ => false
      }
    }

    def gridDrawingInfo: ArrayBuffer[(Color, Int, Int)] = {
      val buffer: ArrayBuffer[(Color, Int, Int)] = new ArrayBuffer()
      _grid.foreach(_.foreach(tile => buffer.addOne(tile.getDrawingInfo)))
      buffer
    }

    def tile(x: Int, y: Int): Tile = _grid(y)(x)

    def grid: Array[Array[Tile]] = _grid

    def tileStartOrEnd(filter: TileTypes.TileType): Option[Tile] = {
      filter match {
        case TileTypes.StartTile | TileTypes.EndTile =>
          _grid.foreach(y => y.foreach(x => if (x.tType.tileType == filter) {
            return Some(Tile(x.x, x.y, TileType(filter)))
          }))
          None
        case _ => None
      }
    }

  }

  def apply(difficulty: Int): Grid = GridImpl(difficulty)
}
