package org.ogzkesk.marvel.test.app.detection

import ai.onnxruntime.OrtSession
import co.touchlab.kermit.Logger

fun OrtSession.logModelInfo(){
    Logger.i(
        "inputNames: ${inputNames.toList()}\n" +
                "inputInfo: ${inputInfo.toMap()}\n" +
                "numInputs: ${numInputs}\n" +
                "outputNames: ${outputNames}\n" +
                "outputInfo: ${outputInfo}\n" +
                "numOutputs: ${numOutputs}" +
                "profilingStartTimeInNs: ${profilingStartTimeInNs}\n\n" +
                "Metadata -------->\n" +
                "\tcustomMetadata: ${metadata.customMetadata.toMap()}\n" +
                "\tdomain: ${metadata.domain}\n" +
                "\tversion: ${metadata.version}\n" +
                "\tgraphName: ${metadata.graphName}\n" +
                "\tdescription: ${metadata.description}\n" +
                "\tgraphDescription: ${metadata.graphDescription}\n" +
                "\tproducerName: ${metadata.producerName}"
    )
}