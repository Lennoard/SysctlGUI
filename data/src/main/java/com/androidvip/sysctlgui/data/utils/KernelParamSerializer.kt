package com.androidvip.sysctlgui.data.utils

import com.androidvip.sysctlgui.data.models.KernelParamDTO
import com.androidvip.sysctlgui.utils.Consts
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

object KernelParamSerializer : KSerializer<KernelParamDTO> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("KernelParamDTO") {
        element<Int>("id")
        element<String>("name")
        element<String>("path")
        element<String>("value")
        element<Boolean>("isFavorite")
        element<Boolean>("isTaskerParam")
        element<Int>("taskerList")
    }

    override fun serialize(encoder: Encoder, value: KernelParamDTO) {
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, 0, value.id)
            encodeStringElement(descriptor, 1, value.name)
            encodeStringElement(descriptor, 2, value.path)
            encodeStringElement(descriptor, 3, value.value)
            encodeBooleanElement(descriptor, 4, value.isFavorite)
            encodeBooleanElement(descriptor, 5, value.isTaskerParam)
            encodeIntElement(descriptor, 6, value.taskerList)
        }
    }

    override fun deserialize(decoder: Decoder): KernelParamDTO {
        return decoder.decodeStructure(descriptor) {
            var id = 0
            var name = ""
            var path = ""
            var value = ""
            var isFavorite = false
            var isTaskerParam = false
            var taskerList = Consts.LIST_NUMBER_PRIMARY_TASKER // Default

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> id = decodeIntElement(descriptor, 0)
                    1 -> name = decodeStringElement(descriptor, 1)
                    2 -> path = decodeStringElement(descriptor, 2)
                    3 -> value = decodeStringElement(descriptor, 3)
                    4 -> isFavorite = decodeBooleanElement(descriptor, 4)
                    5 -> isTaskerParam = decodeBooleanElement(descriptor, 5)
                    6 -> taskerList = decodeIntElement(descriptor, 6)
                    CompositeDecoder.Companion.DECODE_DONE -> break
                    else -> throw SerializationException("Unknown index $index")
                }
            }
            KernelParamDTO(id, name, path, value, isFavorite, isTaskerParam, taskerList)
        }
    }
}