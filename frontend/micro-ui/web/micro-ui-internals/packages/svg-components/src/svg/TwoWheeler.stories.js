import React from "react";
import { TwoWheeler } from "./TwoWheeler";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "TwoWheeler",
  component: TwoWheeler,
};

export const Default = () => <TwoWheeler />;
export const Fill = () => <TwoWheeler fill="blue" />;
export const Size = () => <TwoWheeler height="50" width="50" />;
export const CustomStyle = () => <TwoWheeler style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TwoWheeler className="custom-class" />;
export const Clickable = () => <TwoWheeler onClick={()=>console.log("clicked")} />;

const Template = (args) => <TwoWheeler {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
