import { createBrowserRouter } from "react-router";
import HomeScreen from "./screens/HomeScreen";
import TaskScreen from "./screens/TaskScreen";
import StatsScreen from "./screens/StatsScreen";
import RewardsScreen from "./screens/RewardsScreen";
import SettingsScreen from "./screens/SettingsScreen";
import Layout from "./components/Layout";

export const router = createBrowserRouter([
  {
    path: "/",
    Component: Layout,
    children: [
      { index: true, Component: HomeScreen },
      { path: "tasks", Component: TaskScreen },
      { path: "stats", Component: StatsScreen },
      { path: "rewards", Component: RewardsScreen },
      { path: "settings", Component: SettingsScreen },
    ],
  },
]);
