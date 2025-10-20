import { useRoute } from "@react-navigation/native"
import { Text } from "react-native"

export const DetailPage = () => {

    const {params} = useRoute<any>()

    return <>
        <Text>Detail Page {params.id} </Text>
    </>
}