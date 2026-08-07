import React from "react";
import { LightbulbOutline } from "./LightbulbOutline";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LightbulbOutline",
  component: LightbulbOutline,
};

export const Default = () => <LightbulbOutline />;
export const Fill = () => <LightbulbOutline fill="blue" />;
export const Size = () => <LightbulbOutline height="50" width="50" />;
export const CustomStyle = () => <LightbulbOutline style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LightbulbOutline className="custom-class" />;

export const Clickable = () => <LightbulbOutline onClick={()=>console.log("clicked")} />;

const Template = (args) => <LightbulbOutline {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
