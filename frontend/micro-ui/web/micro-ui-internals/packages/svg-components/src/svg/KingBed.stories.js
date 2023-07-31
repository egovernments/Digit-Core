import React from "react";
import { KingBed } from "./KingBed";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "KingBed",
  component: KingBed,
};

export const Default = () => <KingBed />;
export const Fill = () => <KingBed fill="blue" />;
export const Size = () => <KingBed height="50" width="50" />;
export const CustomStyle = () => <KingBed style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <KingBed className="custom-class" />;

export const Clickable = () => <KingBed onClick={()=>console.log("clicked")} />;

const Template = (args) => <KingBed {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
