import React from "react";
import { DoneOutline } from "./DoneOutline";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "DoneOutline",
  component: DoneOutline,
};

export const Default = () => <DoneOutline />;
export const Fill = () => <DoneOutline fill="blue" />;
export const Size = () => <DoneOutline height="50" width="50" />;
export const CustomStyle = () => <DoneOutline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DoneOutline className="custom-class" />;

export const Clickable = () => <DoneOutline onClick={()=>console.log("clicked")} />;

const Template = (args) => <DoneOutline {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
