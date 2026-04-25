import { Link, useLocation } from "react-router";
import svgPaths from "../../imports/svg-988blia4a4";
import settingsSvgPaths from "../../imports/svg-8j7exj7e0h";

export default function BottomNavigation() {
  const location = useLocation();

  const navItems = [
    { path: "/", label: "Focus", icon: "p2b4d1580", paths: svgPaths },
    { path: "/tasks", label: "Tasks", icon: "p3877b20", paths: svgPaths },
    { path: "/stats", label: "Stats", icon: "p2ee216f0", paths: svgPaths },
    { path: "/rewards", label: "Rewards", icon: "pff7e600", paths: svgPaths },
    { path: "/settings", label: "Settings", icon: "p2cfc1500", paths: settingsSvgPaths },
  ];

  return (
    <div className="backdrop-blur-[12px] bg-[rgba(253,248,253,0.95)] border-t border-[rgba(228,190,186,0.1)] content-stretch flex items-center justify-around px-[8px] py-[4px] shadow-[0px_-2px_8px_0px_rgba(28,27,31,0.08)]">
      {navItems.map((item) => {
        const isActive = location.pathname === item.path;
        return (
          <Link
            key={item.path}
            to={item.path}
            className={`content-stretch flex flex-col items-center justify-center px-[20px] py-[8px] relative rounded-[16px] shrink-0 ${
              isActive ? "bg-[#f1ecf2]" : ""
            }`}
          >
            <div className="h-[28px] relative shrink-0 w-[24.02px]">
              <svg className="absolute block inset-0 size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 30 36">
                <g>
                  <path d={item.paths[item.icon as keyof typeof item.paths]} fill={isActive ? "#AF101A" : "#737373"} />
                </g>
              </svg>
            </div>
            <div className="content-stretch flex flex-col items-start relative shrink-0">
              <div className={`flex flex-col font-['Manrope:Bold',sans-serif] font-bold justify-center leading-[0] relative shrink-0 text-[10px] tracking-[1px] uppercase whitespace-nowrap ${
                isActive ? "text-[#af101a]" : "text-[#737373]"
              }`}>
                <p className="leading-[15px]">{item.label}</p>
              </div>
            </div>
          </Link>
        );
      })}
    </div>
  );
}
