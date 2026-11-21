import React from "react";
import { East } from "./East";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "East",
  component: East,
};

export const Default = () => <East />;
export const Fill = () => <East fill="blue" />;
export const Size = () => <East height="50" width="50" />;
export const CustomStyle = () => <East style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <East className="custom-class" />;

export const Clickable = () => <East onClick={()=>console.log("clicked")} />;

const Template = (args) => <East {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
