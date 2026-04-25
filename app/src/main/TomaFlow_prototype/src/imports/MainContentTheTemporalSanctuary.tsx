import svgPaths from "./svg-falgfrtn0n";
import { imgBefore } from "./svg-uqcd0";

function SpanFontBold() {
  return (
    <div className="content-stretch flex flex-col items-center relative shrink-0 w-full" data-name="span.font-bold">
      <div className="flex flex-col font-['Manrope:Bold',sans-serif] font-bold justify-center leading-[0] relative shrink-0 text-[12px] text-[rgba(175,16,26,0.8)] text-center tracking-[1.2px] uppercase whitespace-nowrap">
        <p className="leading-[18px]">Current Session</p>
      </div>
    </div>
  );
}

function H2FontExtrabold() {
  return (
    <div className="content-stretch flex flex-col items-center relative shrink-0 w-full" data-name="h2.font-extrabold">
      <div className="flex flex-col font-['Manrope:ExtraBold',sans-serif] font-extrabold justify-center leading-[0] relative shrink-0 text-[#1c1b1f] text-[28px] text-center tracking-[-0.7px] whitespace-nowrap">
        <p className="leading-[42px]">Focus Time</p>
      </div>
    </div>
  );
}

function StatusLabel() {
  return (
    <div className="content-stretch flex flex-col gap-[8px] items-start relative shrink-0 w-[149px]" data-name="Status Label">
      <SpanFontBold />
      <H2FontExtrabold />
    </div>
  );
}

function StatusLabelMargin() {
  return (
    <div className="absolute content-stretch flex flex-col items-start left-[121.09px] pb-[48px] top-0" data-name="Status Label:margin">
      <StatusLabel />
    </div>
  );
}

function Icon() {
  return (
    <div className="h-[28px] relative w-[24.02px]" data-name="Icon">
      <svg className="absolute block inset-0 size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 24.02 28">
        <g id="Icon">
          <path d={svgPaths.p14944c00} fill="var(--fill-0, #1B6D24)" id="Vector" />
        </g>
      </svg>
    </div>
  );
}

function SpanMaterialSymbolsOutlined() {
  return (
    <div className="content-stretch flex flex-col items-start relative shrink-0" data-name="span.material-symbols-outlined">
      <div className="flex items-center justify-center relative shrink-0">
        <div className="-scale-y-100 flex-none">
          <Icon />
        </div>
      </div>
    </div>
  );
}

function DivW() {
  return (
    <div className="bg-[rgba(160,243,153,0.3)] relative rounded-[16px] shrink-0 size-[48px]" data-name="div.w-12">
      <div className="bg-clip-padding border-0 border-[transparent] border-solid content-stretch flex items-center justify-center relative size-full">
        <SpanMaterialSymbolsOutlined />
      </div>
    </div>
  );
}

function H3TextOnSurface() {
  return (
    <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="h3.text-on-surface">
      <div className="flex flex-col font-['Manrope:SemiBold',sans-serif] font-semibold justify-center leading-[0] relative shrink-0 text-[#1c1b1f] text-[14px] whitespace-nowrap">
        <p className="leading-[20px]">Design System Review</p>
      </div>
    </div>
  );
}

function PTextXs() {
  return (
    <div className="content-stretch flex flex-col items-start relative shrink-0 w-full" data-name="p.text-xs">
      <div className="flex flex-col font-['Manrope:Regular',sans-serif] font-normal justify-center leading-[0] relative shrink-0 text-[12px] text-[rgba(91,64,61,0.7)] whitespace-nowrap">
        <p className="leading-[16px]">Part of Project Sanctuary</p>
      </div>
    </div>
  );
}

function Div() {
  return (
    <div className="relative shrink-0 w-[150px]" data-name="div">
      <div className="bg-clip-padding border-0 border-[transparent] border-solid content-stretch flex flex-col items-start relative size-full">
        <H3TextOnSurface />
        <PTextXs />
      </div>
    </div>
  );
}

function Icon1() {
  return (
    <div className="h-[28px] relative w-[24.02px]" data-name="Icon">
      <svg className="absolute block inset-0 size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 24.02 28">
        <g id="Icon">
          <path d={svgPaths.p2829b180} fill="var(--fill-0, #AF101A)" id="Vector" />
        </g>
      </svg>
    </div>
  );
}

function SpanMaterialSymbolsOutlined1() {
  return (
    <div className="content-stretch flex items-start relative shrink-0" data-name="span.material-symbols-outlined">
      <div className="flex items-center justify-center relative shrink-0">
        <div className="-scale-y-100 flex-none">
          <Icon1 />
        </div>
      </div>
    </div>
  );
}

function ButtonMlAuto() {
  return (
    <div className="absolute content-stretch flex flex-col items-center justify-center pb-[4px] right-[-0.4px] top-[-2px]" data-name="button.ml-auto">
      <SpanMaterialSymbolsOutlined1 />
    </div>
  );
}

function ButtonMlAutoMargin() {
  return (
    <div className="flex-[1_0_0] h-[30px] min-w-[24.020000457763672px] relative" data-name="button.ml-auto:margin">
      <div className="bg-clip-padding border-0 border-[transparent] border-solid relative size-full">
        <ButtonMlAuto />
      </div>
    </div>
  );
}

function ContextualTaskInsightEditorialLayout() {
  return (
    <div className="bg-white relative rounded-[32px] shrink-0 w-full" data-name="Contextual Task Insight (Editorial Layout)">
      <div aria-hidden="true" className="absolute border border-[rgba(228,190,186,0.1)] border-solid inset-0 pointer-events-none rounded-[32px]" />
      <div className="flex flex-row items-center size-full">
        <div className="content-stretch flex gap-[16px] items-center p-[25px] relative size-full">
          <DivW />
          <Div />
          <ButtonMlAutoMargin />
        </div>
      </div>
    </div>
  );
}

function ContextualTaskInsightEditorialLayoutMargin() {
  return (
    <div className="absolute content-stretch flex flex-col items-start left-[24px] max-w-[384px] pt-[64px] right-[24px] top-[548px]" data-name="Contextual Task Insight (Editorial Layout):margin">
      <ContextualTaskInsightEditorialLayout />
    </div>
  );
}

function ProgressHalo() {
  return (
    <div className="relative size-full" data-name="Progress Halo">
      <svg className="absolute block inset-0 size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 288 288">
        <g id="Progress Halo">
          <path d={svgPaths.p1cddc200} id="Vector" stroke="var(--stroke-0, #FFDAD6)" strokeWidth="11.52" />
          <path d={svgPaths.p1cddc200} id="Vector_2" stroke="var(--stroke-0, #AF101A)" strokeLinecap="round" strokeWidth="11.52" />
        </g>
      </svg>
    </div>
  );
}

function SpanFontExtrabold() {
  return (
    <div className="content-stretch flex flex-col items-center relative shrink-0 w-full" data-name="span.font-extrabold">
      <div className="flex flex-col font-['Manrope:ExtraBold',sans-serif] font-extrabold justify-center leading-[0] relative shrink-0 text-[#1c1b1f] text-[56px] text-center tracking-[-1.12px] whitespace-nowrap">
        <p className="leading-[56px]">25:00</p>
      </div>
    </div>
  );
}

function BeforeMaskGroup() {
  return (
    <div className="-translate-x-1/2 absolute h-[4px] left-1/2 top-[-3px] w-[6px]" data-name="::before:mask-group">
      <div className="-translate-x-1/2 absolute bg-[#217128] h-[4px] left-1/2 mask-alpha mask-intersect mask-no-clip mask-no-repeat mask-position-[0px_0px] mask-size-[6px_4px] rounded-[1px] top-0 w-[6px]" style={{ maskImage: `url('${imgBefore}')` }} data-name="::before" />
    </div>
  );
}

function MinimalTomatoIconReplacingFireIcon() {
  return (
    <div className="bg-[rgba(175,16,26,0.4)] relative rounded-[8.1px] shrink-0 size-[18px]" data-name="Minimal Tomato Icon replacing fire icon">
      <BeforeMaskGroup />
    </div>
  );
}

function SpanFontBold1() {
  return (
    <div className="content-stretch flex flex-col items-center relative shrink-0" data-name="span.font-bold">
      <div className="flex flex-col font-['Manrope:Bold',sans-serif] font-bold justify-center leading-[0] relative shrink-0 text-[12px] text-[rgba(91,64,61,0.6)] text-center tracking-[0.6px] uppercase whitespace-nowrap">
        <p className="leading-[18px]">Deep Work</p>
      </div>
    </div>
  );
}

function DivFlex() {
  return (
    <div className="content-stretch flex gap-[8px] items-center justify-center relative shrink-0 w-full" data-name="div.flex">
      <MinimalTomatoIconReplacingFireIcon />
      <SpanFontBold1 />
    </div>
  );
}

function CountdownDisplay() {
  return (
    <div className="content-stretch flex flex-col gap-[12px] items-start relative shrink-0 w-[158px]" data-name="Countdown Display">
      <SpanFontExtrabold />
      <DivFlex />
    </div>
  );
}

function TimerHaloDisplay() {
  return (
    <div className="content-stretch flex items-center justify-center pb-[105px] pt-[97px] relative shrink-0 size-[288px]" data-name="Timer Halo Display">
      <div className="absolute flex inset-0 items-center justify-center" style={{ containerType: "size" }}>
        <div className="-rotate-90 flex-none h-[100cqw] w-[100cqh]">
          <ProgressHalo />
        </div>
      </div>
      <div className="absolute bg-[rgba(175,16,26,0.05)] blur-[12px] right-[-16px] rounded-[9999px] size-[48px] top-[-16px]" data-name="Subtle backdrop detail" />
      <CountdownDisplay />
    </div>
  );
}

function TimerHaloDisplayMargin() {
  return (
    <div className="absolute content-stretch flex flex-col h-[352px] items-start left-[51px] pb-[64px] top-[116px] w-[288px]" data-name="Timer Halo Display:margin">
      <TimerHaloDisplay />
    </div>
  );
}

function Icon2() {
  return (
    <div className="h-[34px] relative w-[28.02px]" data-name="Icon">
      <svg className="absolute block inset-0 size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 28.02 34">
        <g id="Icon">
          <path d={svgPaths.p3d12900} fill="var(--fill-0, #5B403D)" id="Vector" />
        </g>
      </svg>
    </div>
  );
}

function SpanMaterialSymbolsOutlined2() {
  return (
    <div className="content-stretch flex items-start relative shrink-0" data-name="span.material-symbols-outlined">
      <div className="flex items-center justify-center relative shrink-0">
        <div className="-scale-y-100 flex-none">
          <Icon2 />
        </div>
      </div>
    </div>
  );
}

function ResetButton() {
  return (
    <div className="content-stretch flex flex-col items-center justify-center p-[16px] relative rounded-[9999px] shrink-0" data-name="Reset Button">
      <SpanMaterialSymbolsOutlined2 />
    </div>
  );
}

function Icon3() {
  return (
    <div className="h-[48px] relative w-[40.02px]" data-name="Icon">
      <svg className="absolute block inset-0 size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 40.02 48">
        <g id="Icon">
          <path d={svgPaths.p17834100} fill="var(--fill-0, white)" id="Vector" />
        </g>
      </svg>
    </div>
  );
}

function SpanMaterialSymbolsOutlined3() {
  return (
    <div className="h-[40px] relative shrink-0 w-[40.02px]" data-name="span.material-symbols-outlined">
      <div className="absolute flex h-[48px] items-center justify-center left-0 top-[-4px] w-[40.02px]">
        <div className="-scale-y-100 flex-none">
          <Icon3 />
        </div>
      </div>
    </div>
  );
}

function PlayPausePrimaryAction() {
  return (
    <div className="content-stretch flex items-center justify-center relative rounded-[24px] shrink-0 size-[80px]" style={{ backgroundImage: "linear-gradient(135deg, rgb(175, 16, 26) 0%, rgb(211, 47, 47) 100%)" }} data-name="Play/Pause Primary Action">
      <div className="-translate-y-1/2 absolute bg-[rgba(255,255,255,0)] left-0 rounded-[24px] shadow-[0px_20px_25px_-5px_rgba(175,16,26,0.2),0px_8px_10px_-6px_rgba(175,16,26,0.2)] size-[80px] top-1/2" data-name="Play/Pause Primary Action:shadow" />
      <SpanMaterialSymbolsOutlined3 />
    </div>
  );
}

function Icon4() {
  return (
    <div className="h-[34px] relative w-[28.02px]" data-name="Icon">
      <svg className="absolute block inset-0 size-full" fill="none" preserveAspectRatio="none" viewBox="0 0 28.02 34">
        <g id="Icon">
          <path d={svgPaths.p25d569b0} fill="var(--fill-0, #5B403D)" id="Vector" />
        </g>
      </svg>
    </div>
  );
}

function SpanMaterialSymbolsOutlined4() {
  return (
    <div className="content-stretch flex items-start relative shrink-0" data-name="span.material-symbols-outlined">
      <div className="flex items-center justify-center relative shrink-0">
        <div className="-scale-y-100 flex-none">
          <Icon4 />
        </div>
      </div>
    </div>
  );
}

function SkipButton() {
  return (
    <div className="content-stretch flex flex-col items-center justify-center p-[16px] relative rounded-[9999px] shrink-0" data-name="Skip Button">
      <SpanMaterialSymbolsOutlined4 />
    </div>
  );
}

function ControlsSection() {
  return (
    <div className="-translate-x-1/2 -translate-y-1/2 absolute content-stretch flex gap-[32px] items-center left-1/2 top-[calc(50%+89px)]" data-name="Controls Section">
      <ResetButton />
      <PlayPausePrimaryAction />
      <SkipButton />
    </div>
  );
}

export default function MainContentTheTemporalSanctuary() {
  return (
    <div className="relative size-full" data-name="Main Content: The Temporal Sanctuary">
      <StatusLabelMargin />
      <ContextualTaskInsightEditorialLayoutMargin />
      <TimerHaloDisplayMargin />
      <ControlsSection />
    </div>
  );
}