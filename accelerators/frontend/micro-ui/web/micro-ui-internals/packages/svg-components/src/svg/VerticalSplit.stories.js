import React from "react";
import { VerticalSplit } from "./VerticalSplit";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "VerticalSplit",
  component: VerticalSplit,
};

export const Default = () => <VerticalSplit />;
export const Fill = () => <VerticalSplit fill="blue" />;
export const Size = () => <VerticalSplit height="50" width="50" />;
export const CustomStyle = () => <VerticalSplit style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <VerticalSplit className="custom-class" />;
export const Clickable = () => <VerticalSplit onClick={()=>console.log("clicked")} />;

const Template = (args) => <VerticalSplit {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
