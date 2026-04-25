import svgPaths from "./svg-epvqr9hwxn";
import imgUserProfile from "figma:asset/0b9b2c122a9518716e18cb8cb5daf479d52f11ff.png";

function Icon() {
  return (
    <div className="h-[28px] relative w-[24.02px]" data-name="Icon">
      <svg className="absolute block inset-0 size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 24.02 28">
        <g id="Icon">
          <path d={svgPaths.pc02100} fill="var(--fill-0, #737373)" id="Vector" />
        </g>
      </svg>
    </div>
  );
}

function SpanMaterialSymbolsOutlined() {
  return (
    <div className="content-stretch flex items-start relative shrink-0" data-name="span.material-symbols-outlined">
      <div className="flex items-center justify-center relative shrink-0">
        <div className="-scale-y-100 flex-none">
          <Icon />
        </div>
      </div>
    </div>
  );
}

function ButtonP() {
  return (
    <div className="content-stretch flex flex-col items-center justify-center pb-[12px] pt-[6px] px-[8px] relative rounded-[9999px] shrink-0" data-name="button.p-2">
      <SpanMaterialSymbolsOutlined />
    </div>
  );
}

function DivRoundedFullMargin() {
  return (
    <div className="content-stretch flex flex-col h-[12px] items-start pb-[2px] relative shrink-0 w-[10px]" data-name="div.rounded-full:margin">
      <div className="bg-[#af101a] rounded-[9999px] shrink-0 size-[10px]" data-name="div.rounded-full" />
    </div>
  );
}

function H1TextXl() {
  return (
    <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="h1.text-xl">
      <div className="flex flex-col font-['Manrope:Bold',sans-serif] font-bold justify-center leading-[0] relative shrink-0 text-[#af101a] text-[20px] tracking-[-0.4px] whitespace-nowrap">
        <p className="leading-[28px]">TomaFlow</p>
      </div>
    </div>
  );
}

function DivFlex1() {
  return (
    <div className="content-stretch flex gap-[6px] items-center relative shrink-0" data-name="div.flex">
      <DivRoundedFullMargin />
      <H1TextXl />
    </div>
  );
}

function DivFlex() {
  return (
    <div className="content-stretch flex gap-[16px] items-center relative shrink-0" data-name="div.flex">
      <ButtonP />
      <DivFlex1 />
    </div>
  );
}

function UserProfile() {
  return (
    <div className="flex-[1_0_0] min-h-px relative w-full" data-name="User profile">
      <div className="absolute bg-clip-padding border-0 border-[transparent] border-solid inset-0 overflow-hidden pointer-events-none">
        <img alt="" className="absolute left-0 max-w-none size-full top-0" src={imgUserProfile} />
      </div>
    </div>
  );
}

function DivH() {
  return (
    <div className="relative rounded-[9999px] shrink-0 size-[40px]" data-name="div.h-10">
      <div className="content-stretch flex flex-col items-start justify-center overflow-clip p-px relative rounded-[inherit] size-full">
        <UserProfile />
      </div>
      <div aria-hidden="true" className="absolute border border-[rgba(228,190,186,0.2)] border-solid inset-0 pointer-events-none rounded-[9999px]" />
    </div>
  );
}

export default function TopAppBar() {
  return (
    <div className="bg-[#fdf8fd] content-stretch flex gap-[152.7px] items-center px-[16px] py-[8px] relative size-full" data-name="TopAppBar">
      <DivFlex />
      <DivH />
    </div>
  );
}