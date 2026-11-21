import React from "react";
import { FitScreen } from "./FitScreen";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "FitScreen",
  component: FitScreen,
};

export const Default = () => <FitScreen />;
export const Fill = () => <FitScreen fill="blue" />;
export const Size = () => <FitScreen height="50" width="50" />;
export const CustomStyle = () => <FitScreen style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <FitScreen className="custom-class" />;

export const Clickable = () => <FitScreen onClick={()=>console.log("clicked")} />;

const Template = (args) => <FitScreen {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
