package idv.bruce.common.tools

import android.media.MediaCodecList


object MediaTools {
    fun getSupportCodec(): Pair<List<String>, List<String>> {
        val list = MediaCodecList(MediaCodecList.REGULAR_CODECS)
        val codecs = list.codecInfos

        val encoderList: ArrayList<String> = ArrayList()

        val decoderList: ArrayList<String> = ArrayList()

        for (codec in codecs) {
            if (codec.isEncoder)
                encoderList.add(codec.name)
            else
                decoderList.add(codec.name)
        }

        return Pair(encoderList, decoderList)
    }
}