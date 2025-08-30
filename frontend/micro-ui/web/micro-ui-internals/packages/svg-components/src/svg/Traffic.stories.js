import React from "react";
import { Traffic } from "./Traffic";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Traffic",
  component: Traffic,
};

export const Default = () => <Traffic />;
export const Fill = () => <Traffic fill="blue" />;
export const Size = () => <Traffic height="50" width="50" />;
export const CustomStyle = () => <Traffic style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Traffic className="custom-class" />;
export const Clickable = () => <Traffic onClick={()=>console.log("clicked")} />;

const Template = (args) => <Traffic {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};

