import React from "react";
import { Nightlife } from "./Nightlife";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Nightlife",
  component: Nightlife,
};

export const Default = () => <Nightlife />;
export const Fill = () => <Nightlife fill="blue" />;
export const Size = () => <Nightlife height="50" width="50" />;
export const CustomStyle = () => <Nightlife style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Nightlife className="custom-class" />;

export const Clickable = () => <Nightlife onClick={()=>console.log("clicked")} />;

const Template = (args) => <Nightlife {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
