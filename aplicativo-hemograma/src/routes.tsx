import { createNativeStackNavigator } from "@react-navigation/native-stack"
import { HomePage } from "./core/navigation/Home";
import { DetailPage } from "./core/navigation/Detail";
import { UserNamePage } from "./core/navigation/SetUseName";
import { NavigationContainer } from "@react-navigation/native";


const Stack = createNativeStackNavigator();

export const AppRoutes = () => {
    return (
        <NavigationContainer>
            <Stack.Navigator>
                <Stack.Screen name="Home" component={HomePage} />
                <Stack.Screen name="Detail" component={DetailPage} />
                <Stack.Screen name="Username" component={UserNamePage} />
            </Stack.Navigator>
        </NavigationContainer>
    )
}