import React from "react";
import { RamenDining } from "./RamenDining";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "RamenDining",
  component: RamenDining,
};

export const Default = () => <RamenDining />;
export const Fill = () => <RamenDining fill="blue" />;
export const Size = () => <RamenDining height="50" width="50" />;
export const CustomStyle = () => <RamenDining style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <RamenDining className="custom-class" />;

export const Clickable = () => <RamenDining onClick={()=>console.log("clicked")} />;

const Template = (args) => <RamenDining {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
