import zio.blocks.schema.*
import io.github.iltotore.iron.*

object SchemaIron:
  
  inline given [A, C](using 
    baseSchema: Schema[A]
  ): Schema[A :| C] =
    baseSchema.asInstanceOf[Schema[A :| C]]
