import React from "react";
import { EvStation } from "./EvStation";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "EvStation",
  component: EvStation,
};

export const Default = () => <EvStation />;
export const Fill = () => <EvStation fill="blue" />;
export const Size = () => <EvStation height="50" width="50" />;
export const CustomStyle = () => <EvStation style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <EvStation className="custom-class" />;

export const Clickable = () => <EvStation onClick={()=>console.log("clicked")} />;

const Template = (args) => <EvStation {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
