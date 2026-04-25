import { Outlet } from "react-router";
import TopAppBar from "../../imports/TopAppBar";
import BottomNavigation from "./BottomNavigation";

export default function Layout() {
  return (
    <div className="flex flex-col h-screen bg-[#fdf8fd] relative">
      <div className="shrink-0">
        <TopAppBar />
      </div>
      <div className="flex-1 overflow-auto pb-[100px]">
        <Outlet />
      </div>
      <div className="fixed bottom-0 left-0 right-0 z-50">
        <BottomNavigation />
      </div>
    </div>
  );
}
