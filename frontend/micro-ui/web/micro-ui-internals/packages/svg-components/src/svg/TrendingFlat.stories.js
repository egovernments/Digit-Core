import React from "react";
import { TrendingFlat } from "./TrendingFlat";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "TrendingFlat",
  component: TrendingFlat,
};

export const Default = () => <TrendingFlat />;
export const Fill = () => <TrendingFlat fill="blue" />;
export const Size = () => <TrendingFlat height="50" width="50" />;
export const CustomStyle = () => <TrendingFlat style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TrendingFlat className="custom-class" />;
export const Clickable = () => <TrendingFlat onClick={()=>console.log("clicked")} />;

const Template = (args) => <TrendingFlat {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
