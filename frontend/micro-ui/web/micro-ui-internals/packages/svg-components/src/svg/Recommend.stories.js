import React from "react";
import { Recommend } from "./Recommend";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Recommend",
  component: Recommend,
};

export const Default = () => <Recommend />;
export const Fill = () => <Recommend fill="blue" />;
export const Size = () => <Recommend height="50" width="50" />;
export const CustomStyle = () => <Recommend style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Recommend className="custom-class" />;

export const Clickable = () => <Recommend onClick={()=>console.log("clicked")} />;

const Template = (args) => <Recommend {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
