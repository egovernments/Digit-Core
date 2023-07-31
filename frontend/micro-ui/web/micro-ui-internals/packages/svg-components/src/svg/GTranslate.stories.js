import React from "react";
import { GTranslate } from "./GTranslate";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "GTranslate",
  component: GTranslate,
};

export const Default = () => <GTranslate />;
export const Fill = () => <GTranslate fill="blue" />;
export const Size = () => <GTranslate height="50" width="50" />;
export const CustomStyle = () => <GTranslate style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <GTranslate className="custom-class" />;

export const Clickable = () => <GTranslate onClick={()=>console.log("clicked")} />;

const Template = (args) => <GTranslate {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
