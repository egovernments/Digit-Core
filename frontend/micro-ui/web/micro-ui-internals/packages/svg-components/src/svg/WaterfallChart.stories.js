import React from "react";
import { WaterfallChart } from "./WaterfallChart";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "WaterfallChart",
  component: WaterfallChart,
};

export const Default = () => <WaterfallChart />;
export const Fill = () => <WaterfallChart fill="blue" />;
export const Size = () => <WaterfallChart height="50" width="50" />;
export const CustomStyle = () => <WaterfallChart style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <WaterfallChart className="custom-class" />;
export const Clickable = () => <WaterfallChart onClick={()=>console.log("clicked")} />;

const Template = (args) => <WaterfallChart {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
