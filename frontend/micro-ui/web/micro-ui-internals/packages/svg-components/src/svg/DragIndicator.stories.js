import React from "react";
import { DragIndicator } from "./DragIndicator";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DragIndicator",
  component: DragIndicator,
};

export const Default = () => <DragIndicator />;
export const Fill = () => <DragIndicator fill="blue" />;
export const Size = () => <DragIndicator height="50" width="50" />;
export const CustomStyle = () => <DragIndicator style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DragIndicator className="custom-class" />;

export const Clickable = () => <DragIndicator onClick={()=>console.log("clicked")} />;

const Template = (args) => <DragIndicator {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
