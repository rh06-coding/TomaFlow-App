import { useState } from "react";
import svgPaths from "../../imports/svg-falgfrtn0n";

interface Task {
  id: string;
  title: string;
  completed: boolean;
  tag: string;
}

export default function TaskScreen() {
  const [tasks, setTasks] = useState<Task[]>([
    { id: "1", title: "Design System Review", completed: false, tag: "Design" },
    { id: "2", title: "API Documentation", completed: false, tag: "Dev" },
    { id: "3", title: "User Research Analysis", completed: true, tag: "Research" },
    { id: "4", title: "Component Library Update", completed: false, tag: "Design" },
  ]);
  const [newTaskTitle, setNewTaskTitle] = useState("");

  const toggleTask = (id: string) => {
    setTasks(tasks.map(task =>
      task.id === id ? { ...task, completed: !task.completed } : task
    ));
  };

  const addTask = () => {
    if (newTaskTitle.trim()) {
      setTasks([...tasks, {
        id: Date.now().toString(),
        title: newTaskTitle,
        completed: false,
        tag: "General"
      }]);
      setNewTaskTitle("");
    }
  };

  return (
    <div className="min-h-full px-[24px] py-[32px]">
      <div className="mb-[24px]">
        <div className="flex flex-col font-['Manrope:Bold',sans-serif] font-bold justify-center leading-[0] text-[12px] text-[rgba(175,16,26,0.8)] tracking-[1.2px] uppercase mb-[8px]">
          <p className="leading-[18px]">My Tasks</p>
        </div>
        <div className="flex flex-col font-['Manrope:ExtraBold',sans-serif] font-extrabold justify-center leading-[0] text-[#1c1b1f] text-[28px] tracking-[-0.7px]">
          <p className="leading-[42px]">To-Do List</p>
        </div>
      </div>

      <div className="mb-[24px]">
        <div className="bg-white rounded-[24px] p-[16px] border border-[rgba(228,190,186,0.1)]">
          <input
            type="text"
            value={newTaskTitle}
            onChange={(e) => setNewTaskTitle(e.target.value)}
            onKeyPress={(e) => e.key === "Enter" && addTask()}
            placeholder="Add a new task..."
            className="w-full bg-transparent font-['Manrope:SemiBold',sans-serif] font-semibold text-[14px] text-[#1c1b1f] outline-none placeholder:text-[rgba(91,64,61,0.5)]"
          />
        </div>
      </div>

      <div className="space-y-[12px]">
        {tasks.map((task) => (
          <div
            key={task.id}
            className="bg-white rounded-[24px] p-[20px] border border-[rgba(228,190,186,0.1)] flex items-center gap-[16px]"
          >
            <button
              onClick={() => toggleTask(task.id)}
              className="shrink-0"
            >
              <div className={`w-[24px] h-[24px] rounded-full border-2 flex items-center justify-center ${
                task.completed ? "bg-[#1B6D24] border-[#1B6D24]" : "border-[rgba(91,64,61,0.3)]"
              }`}>
                {task.completed && (
                  <svg className="w-[14px] h-[14px]" fill="none" viewBox="0 0 24 28">
                    <path d={svgPaths.p14944c00} fill="white" />
                  </svg>
                )}
              </div>
            </button>
            <div className="flex-1">
              <div className={`flex flex-col font-['Manrope:SemiBold',sans-serif] font-semibold justify-center leading-[0] text-[14px] ${
                task.completed ? "text-[rgba(91,64,61,0.5)] line-through" : "text-[#1c1b1f]"
              }`}>
                <p className="leading-[20px]">{task.title}</p>
              </div>
              <div className="flex flex-col font-['Manrope:Regular',sans-serif] font-normal justify-center leading-[0] text-[12px] text-[rgba(91,64,61,0.7)] mt-[4px]">
                <p className="leading-[16px]">{task.tag}</p>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
