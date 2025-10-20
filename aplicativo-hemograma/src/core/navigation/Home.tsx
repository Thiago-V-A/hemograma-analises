import { Button, FlatList, Text } from "react-native"
import { mainStyle } from "../styles/common"
import { useNavigation } from "@react-navigation/native"
import { useState } from "react"
import { NativeStackNavigationProp } from "@react-navigation/native-stack"

type TNavigationScreenProps = NativeStackNavigationProp<Record<string, any>>

export const HomePage = () => {

    const navigation = useNavigation<TNavigationScreenProps>();

    const [list, setList] = useState([
        1, 2, 3, 4, 5,
        1, 2, 3, 4, 5,
        1, 2, 3, 4, 5,
        1, 2, 3, 4, 5,
        1, 2, 3, 4, 5,
        1, 2, 3, 4, 5,
        1, 2, 3, 4, 5,
        1, 2, 3, 4, 5,
        1, 2, 3, 4, 5,
        1, 2, 3, 4, 5,
        1, 2, 3, 4, 5,
        1, 2, 3, 4, 5,
        1, 2, 3, 4, 5,
        1, 2, 3, 4, 5,
        1, 2, 3, 4, 5,
        1, 2, 3, 4, 5,
        1, 2, 3, 4, 5,

    ])

    return <>
        <Text style={mainStyle.button}>Home Page</Text>

        <FlatList data={list} renderItem={({ item, index }) => <Text>{item}</Text>} />

        <Button title="Go to Getails" onPress={() => navigation.navigate('Detail', { id: "ADSSDASD"})} />
    </>
}