import React from "react";
import { Liquor } from "./Liquor";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Liquor",
  component: Liquor,
};

export const Default = () => <Liquor />;
export const Fill = () => <Liquor fill="blue" />;
export const Size = () => <Liquor height="50" width="50" />;
export const CustomStyle = () => <Liquor style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Liquor className="custom-class" />;

export const Clickable = () => <Liquor onClick={()=>console.log("clicked")} />;

const Template = (args) => <Liquor {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
