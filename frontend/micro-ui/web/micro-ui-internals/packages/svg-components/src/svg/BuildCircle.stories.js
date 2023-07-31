import React from "react";
import { BuildCircle } from "./BuildCircle";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "BuildCircle",
  component: BuildCircle,
};

export const Default = () => <BuildCircle />;
export const Fill = () => <BuildCircle fill="blue" />;
export const Size = () => <BuildCircle height="50" width="50" />;
export const CustomStyle = () => <BuildCircle style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <BuildCircle className="custom-class" />;

export const Clickable = () => <BuildCircle onClick={()=>console.log("clicked")} />;

const Template = (args) => <BuildCircle {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
