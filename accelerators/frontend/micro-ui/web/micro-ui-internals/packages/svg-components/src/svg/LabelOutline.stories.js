import React from "react";
import { LabelOutline } from "./LabelOutline";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LabelOutline",
  component: LabelOutline,
};

export const Default = () => <LabelOutline />;
export const Fill = () => <LabelOutline fill="blue" />;
export const Size = () => <LabelOutline height="50" width="50" />;
export const CustomStyle = () => <LabelOutline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LabelOutline className="custom-class" />;

export const Clickable = () => <LabelOutline onClick={()=>console.log("clicked")} />;

const Template = (args) => <LabelOutline {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
